import mysql.connector as sql
import os
import uuid

# Replace with correct database credentials. Current credentials only work on the default localhost database.
DB_HOST = "localhost"
DB_USER = "clover"
DB_PASSWORD = "clover"
DB_NAME = "clover"


def load_products():
    def prepare_images():
        png_files = [file for file in os.listdir() if file.endswith('.png')]

        png_blobs = {}
        for file in png_files:
            with open(file, "rb") as binary_file:
                png_blobs[file] = binary_file.read()

        return png_blobs

    images = prepare_images()
    return [
        (str(uuid.uuid4()), 'Crown', 'ACCESSORIES', 800, images["afbeelding_1.png"], None, False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Bowtie', 'ACCESSORIES', 600, images["afbeelding_2.png"], None, False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Eyepatch', 'ACCESSORIES', 800, images["afbeelding_3.png"], None, False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Peach', 'BASE_COLORS', 800, None, "#F19A60", True, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Salmon', 'BASE_COLORS', 200, None, "#C97272", False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Steel Blue', 'BASE_COLORS', 300, None, "#678EB5", False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Supernova', 'BASE_COLORS', 300, None, "#F2C900", False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Royal Blue', 'TRIM_COLORS', 200, None, "#5A86D7", True, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Pea', 'TRIM_COLORS', 300, None, "#186544", False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Amethyst', 'TRIM_COLORS', 300, None, "#A146D6", False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Eggplant', 'TRIM_COLORS', 200, None, "#A0188E", False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Classic', 'TABLE_DECOR', 500, images["afbeelding_4.png"], None, True, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Picnic', 'TABLE_DECOR', 600, images["afbeelding_5.png"], None, False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Cozy Kitchen', 'TABLE_DECOR', 800, images["afbeelding_6.png"], None, False, None, 'LEAFS'),
        (str(uuid.uuid4()), 'Sapling Bundle', 'CURRENCY', 4.99, images["afbeelding_7.png"], None, False, 500, 'EUR'),
        (str(uuid.uuid4()), 'Grove Pack', 'CURRENCY', 9.99, images["afbeelding_8.png"], None, False, 3000, 'EUR'),
        (str(uuid.uuid4()), 'Canopy Cache', 'CURRENCY', 19.99, images["afbeelding_9.png"], None, False, 6500, 'EUR'),
        (str(uuid.uuid4()), 'Forest Treasury', 'CURRENCY', 99.99, images["afbeelding_10.png"], None, False, 50000, 'EUR')
    ]


if __name__ == "__main__":
    insert_query = "INSERT INTO " \
    + "product (`id`, `name`, `category`, `amount`, `created`, `updated`, `image`, `color`, `standard`, `leafs`, `currency`) " \
    + "VALUES (UUID_TO_BIN(%s), %s, %s, %s, CURDATE(), CURDATE(), %s, %s, %s, %s, %s)"

    connection = sql.connect(
        host=DB_HOST,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME
    )
    cursor = connection.cursor()

    try:
        for product in load_products():
            cursor.execute(insert_query, product)

        connection.commit()
    except Exception as e:
        print(f"An error occurred: {e}")
        connection.rollback()
    finally:
        connection.close()